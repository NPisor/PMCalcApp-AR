package com.example.projectmanagement_calcapp;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class AnchorTask {

    Long id;
    String message;
    Long assignerId;
    Long assigneeId;
    String taskDescription;
    Long priority;
    Timestamp createdTs;
    Timestamp submittedForReviewTs;
    Timestamp completedTs;
    Long status;
    Long clientId;
    String taskImageUrl;

    public AnchorTask(Long id, String message, Long assignerId, Long assigneeId, String taskDescription, Long priority, Timestamp createdTs, Timestamp submittedForReviewTs, Timestamp completedTs, Long status, Long clientId, String taskImageUrl) {
        this.id = id;
        this.message = message;
        this.assignerId = assignerId;
        this.assigneeId = assigneeId;
        this.taskDescription = taskDescription;
        this.priority = priority;
        this.createdTs = createdTs;
        this.submittedForReviewTs = submittedForReviewTs;
        this.completedTs = completedTs;
        this.status = status;
        this.clientId = clientId;
        this.taskImageUrl = taskImageUrl;
    }


}
