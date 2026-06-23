package com.flightapp.backend.flights

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty

data class ImportFlightTrackRequest(

    @field:Min(1)
    val formatVersion: Int = 1,

    @field:NotEmpty
    val timeSec: List<Int>,

    @field:NotEmpty
    val latE7: List<Int>,

    @field:NotEmpty
    val lonE7: List<Int>,

    @field:NotEmpty
    val altGpsCm: List<Int>,

    @field:NotEmpty
    val altBaroCm: List<Int>
) {
    fun pointCount(): Int = timeSec.size

    fun validateSameLength() {
        val count = timeSec.size

        if (
            latE7.size != count ||
            lonE7.size != count ||
            altGpsCm.size != count ||
            altBaroCm.size != count
        ) {
            throw IllegalArgumentException("Track arrays must have the same length.")
        }
    }
}