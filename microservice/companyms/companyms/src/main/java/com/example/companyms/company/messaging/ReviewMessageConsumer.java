package com.example.companyms.company.messaging;

import com.example.companyms.company.CompanyService;
import com.example.companyms.company.dto.ReviewMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ReviewMessageConsumer {
    // This class will consume messages from the RabbitMQ queue
    // and process them accordingly.

    private final CompanyService companyService;

    public ReviewMessageConsumer(CompanyService companyService) {
        this.companyService = companyService;
    }

    // Method to consume messages from the queue
    // and update the company's rating based on the review

    @RabbitListener(queues = "companyRatingQueue")
    public void consumeMessage(ReviewMessage reviewMessage) {
         companyService.updateCompanyRating(reviewMessage);
    }
}
