package com.example.file.dto;

import java.time.OffsetDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileDTO
{

    private String id;
    private Long version;
    private OffsetDateTime createdDate;
    private OffsetDateTime lastModifiedDate;
    private String contentType;
    private String filename;
    private String accountId;
    private Long size;
    private String externalId;

}
