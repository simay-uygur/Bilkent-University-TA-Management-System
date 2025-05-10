package com.example.dto;


import com.example.entity.General.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDto {
    private DateDto start;
    private DateDto finish;

    public Event toEntity() {
        return new Event(
                start.toEntity(),
                finish.toEntity()
        );
    }
}