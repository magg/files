package com.example.file.mapper;

import com.example.file.domain.AccountDomain;
import com.example.file.dto.AccountDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@SuppressWarnings("unused")
public interface AccountMapper extends ObjectConverter<AccountDomain, AccountDTO>
{

}