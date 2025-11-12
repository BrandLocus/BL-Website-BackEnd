package com.hvc.brandlocus.entities;

import com.hvc.brandlocus.enums.ChatType;
import com.hvc.brandlocus.enums.SenderType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_message")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ChatSession chatSession;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType sender;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatType chatType;

    @Column(name = "status")
    private String status;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
}
