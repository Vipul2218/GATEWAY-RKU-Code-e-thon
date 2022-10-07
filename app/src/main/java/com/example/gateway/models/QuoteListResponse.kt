package com.example.gateway.models

data class QuoteListResponse (
    val count: Int,
    val lastItemIndex: Int,
    val page: Int,
    val results: List<QuoteData>,
    val totalCount: Int,
    val totalPages: Int
)