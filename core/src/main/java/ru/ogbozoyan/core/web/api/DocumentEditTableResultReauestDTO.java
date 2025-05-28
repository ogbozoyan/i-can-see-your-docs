package ru.ogbozoyan.core.web.api;

import ru.ogbozoyan.core.dao.entity.TableBig;
import ru.ogbozoyan.core.dao.entity.TableSmall;

public record DocumentEditTableResultReauestDTO(TableBig tableBig, TableSmall tableSmall) {}
