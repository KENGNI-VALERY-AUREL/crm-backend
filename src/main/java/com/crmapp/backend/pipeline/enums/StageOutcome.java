package com.crmapp.backend.pipeline.enums;

/**
 * Nature of a pipeline stage (cahier des charges, section 7.5): a normal
 * in-progress stage, or one of the two terminal outcomes.
 */
public enum StageOutcome {
    OPEN,
    WON,
    LOST
}
