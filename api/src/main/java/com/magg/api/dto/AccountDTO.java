package com.magg.api.dto;

import java.time.OffsetDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AccountDTO
{
    private String id;
    private Long version;
    private OffsetDateTime createdDate;
    private OffsetDateTime lastModifiedDate;
    private String name;
    private String emailAddress;
}
