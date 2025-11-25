package com.example.berotravel20.data.models.Journey;

import java.util.List;

public class JourneyResponse {
    private boolean success;
    private List<Journey> data;

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public List<Journey> getData() { return data; }
    public void setData(List<Journey> data) { this.data = data; }
}
