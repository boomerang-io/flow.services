// package io.boomerang.core;
//
// import io.boomerang.core.entity.QueueEventEntity;
// import io.boomerang.core.repository.QueueEventRepository;
// import org.springframework.context.ApplicationEventPublisher;
// import org.springframework.stereotype.Service;
//
// @Service
// public class QueuePublisherService {
//
//  private final ApplicationEventPublisher eventPublisher;
//  private final QueueEventRepository eventRepository;
//
//  public QueuePublisherService(
//      ApplicationEventPublisher eventPublisher, QueueEventRepository eventRepository) {
//    this.eventPublisher = eventPublisher;
//    this.eventRepository = eventRepository;
//  }
//
//  public void publishEvent(Object event) {
//    // Persist the event in MongoDB
//    QueueEventEntity eventEntity =
//        new QueueEventEntity(event.getClass().getSimpleName(), event.toString());
//    eventRepository.save(eventEntity);
//
//    // Publish the event
//    eventPublisher.publishEvent(event);
//  }
// }
