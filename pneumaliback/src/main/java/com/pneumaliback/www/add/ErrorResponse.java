package com.pneumaliback.www.add;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    private String error;
    private boolean success = false;

    public ErrorResponse(String error) {
        this.error = error;
    }
}
