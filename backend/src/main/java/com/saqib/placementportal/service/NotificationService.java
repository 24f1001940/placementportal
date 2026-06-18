package com.saqib.placementportal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saqib.placementportal.config.RabbitConfig;
import com.saqib.placementportal.dto.ApiDtos.NotificationMessage;
import com.saqib.placementportal.dto.ApiDtos.NotificationResponse;
import com.saqib.placementportal.entity.Notification;
import com.saqib.placementportal.entity.RoleName;
import com.saqib.placementportal.entity.UserAccount;
import com.saqib.placementportal.exception.ForbiddenActionException;
import com.saqib.placementportal.exception.ResourceNotFoundException;
import com.saqib.placementportal.repository.NotificationRepository;
import com.saqib.placementportal.repository.UserRepository;
import com.saqib.placementportal.util.ApiMapper;
import java.util.List;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            RabbitTemplate rabbitTemplate,
            ObjectMapper objectMapper
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    public void send(UserAccount user, String title, String message) {
        NotificationMessage notification = new NotificationMessage(user.getId(), title, message);
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.NOTIFICATION_EXCHANGE,
                    RabbitConfig.NOTIFICATION_ROUTING_KEY,
                    objectMapper.writeValueAsString(notification)
            );
        } catch (Exception ex) {
            save(notification);
        }
    }

    public void notifyAdmins(String title, String message) {
        userRepository.search(null, RoleName.ADMIN, org.springframework.data.domain.Pageable.unpaged())
                .forEach(admin -> send(admin, title, message));
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICATION_QUEUE)
    public void consume(String payload) throws Exception {
        save(objectMapper.readValue(payload, NotificationMessage.class));
    }

    @Transactional
    public void save(NotificationMessage notification) {
        UserAccount user = userRepository.findById(notification.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification user not found"));
        notificationRepository.save(new Notification(user, notification.title(), notification.message()));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> mine() {
        UserAccount user = currentUserService.user();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
                .map(ApiMapper::notification)
                .toList();
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        UserAccount user = currentUserService.user();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new ForbiddenActionException("Cannot update another user's notification");
        }
        notification.markRead();
        return ApiMapper.notification(notificationRepository.save(notification));
    }
}
