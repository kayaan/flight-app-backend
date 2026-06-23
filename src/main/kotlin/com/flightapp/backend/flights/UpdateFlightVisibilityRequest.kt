package com.flightapp.backend.flights

import jakarta.validation.constraints.NotNull

data class UpdateFlightVisibilityRequest(

    @field:NotNull
    val visibility: FlightVisibility
)