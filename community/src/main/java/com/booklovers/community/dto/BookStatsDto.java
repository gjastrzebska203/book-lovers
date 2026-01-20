package com.booklovers.community.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookStatsDto {
    private Double averageRating;   
    private Long ratingCount;    
    private Long readerCount;

    private Map<Integer, Long> ratingDistribution; 
    
    public int getPercentage(int star) {
        if (ratingCount == 0) return 0;
        long count = ratingDistribution.getOrDefault(star, 0L);
        return (int) ((count * 100) / ratingCount);
    }
}
