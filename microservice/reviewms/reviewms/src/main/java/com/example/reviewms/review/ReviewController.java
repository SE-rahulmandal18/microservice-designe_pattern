package com.example.reviewms.review;

import com.example.reviewms.review.messaging.ReviewMessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    private final ReviewMessageProducer reviewMessageProducer;

    public ReviewController(ReviewService reviewService, ReviewMessageProducer reviewMessageProducer) {
        this.reviewService = reviewService;
        this.reviewMessageProducer = reviewMessageProducer;
    }

    //Getting All Reviews Of a Specific Company
    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews(@RequestParam Long companyId) {
        return new ResponseEntity<>(reviewService.getAllReviews(companyId), HttpStatus.OK);
    }

    //Add a review for a specific company
    @PostMapping
    public ResponseEntity<String> addReview(@RequestParam Long companyId, @RequestBody Review review) {

        boolean isReviewSaved = reviewService.addReview(companyId, review);
        if (isReviewSaved){
            reviewMessageProducer.sendMessage(review);
            return new ResponseEntity<>("Review Added successfully",HttpStatus.OK);
        }else
            return new ResponseEntity<>("Review not saved", HttpStatus.NOT_FOUND);
    }

    //Get a review by review ID for a specific company
    @GetMapping("/{reviewId}")
    public ResponseEntity<Review> getReview(@PathVariable Long reviewId) {

        return new ResponseEntity<>(reviewService.getReview(reviewId), HttpStatus.OK);
    }

    //Delete a review by review ID for a specific company
    @DeleteMapping("{reviewId}")
    public ResponseEntity<String> deleteReview(@PathVariable Long reviewId) {

        boolean isReviewDeleted = reviewService.deleteReview( reviewId);
        if (isReviewDeleted)
            return new ResponseEntity<>("Review Deleted successfully", HttpStatus.OK);
        else
            return new ResponseEntity<>("Review to be Deleted not found", HttpStatus.NOT_FOUND);
    }

    //Update a review by review ID for a specific company
    @PutMapping("/{reviewId}")
    public ResponseEntity<String> updateReview(@PathVariable Long reviewId,
                                               @RequestBody Review review) {

        boolean isReviewUpdated = reviewService.updateReview(reviewId, review);

        if (isReviewUpdated)
            return new ResponseEntity<>("Review Updated Successfully", HttpStatus.OK);
        else
            return new ResponseEntity<>("Review not updated", HttpStatus.NOT_FOUND);
    }

    @GetMapping("/averageRating")
    public Double getAverageReview(@RequestParam Long companyId) {
    List<Review> reviewList = reviewService.getAllReviews(companyId);
        return reviewList.stream().mapToDouble(Review::getRating).average()
                .orElse(0.0);
    }
}
