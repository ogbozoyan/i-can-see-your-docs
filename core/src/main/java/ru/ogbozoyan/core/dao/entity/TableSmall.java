package ru.ogbozoyan.core.dao.entity;

import java.util.List;

public record TableSmall(
    TableNamesEnum tableName,

    List<TableColumnResultsWithProbability> probabilitiesForColumn_1,

    List<TableColumnResultsWithProbability> probabilitiesForColumn_2,

    List<TableColumnResultsWithProbability> probabilitiesForColumn_3,

    List<TableColumnResultsWithProbability> probabilitiesForColumn_4,

    List<TableColumnResultsWithProbability> probabilitiesForColumn_5,

    List<TableColumnResultsWithProbability> probabilitiesForColumn_6,

    List<TableColumnResultsWithProbability> probabilitiesForColumn_7
) {
}
