package uk.ac.ed.acp.cw2.data.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter@Setter
public class QueryRequest {
    private String attribute;
    private String operator;
    private String value;

    }

