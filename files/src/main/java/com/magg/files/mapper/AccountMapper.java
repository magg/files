package com.magg.files.mapper;

import com.magg.files.domain.AccountDomain;
import com.magg.api.dto.AccountDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@SuppressWarnings("unused")
public interface AccountMapper extends ObjectConverter<AccountDomain, AccountDTO>
{

}