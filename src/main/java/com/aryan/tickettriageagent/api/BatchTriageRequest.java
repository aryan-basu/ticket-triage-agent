package com.aryan.tickettriageagent.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record BatchTriageRequest(@Min(1) @Max(50) Integer limit) { }
