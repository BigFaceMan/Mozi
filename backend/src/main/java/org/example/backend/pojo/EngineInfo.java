package org.example.backend.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EngineInfo {
    private int engineId;
    private String nodeName;
    private String ip;
    private String port;
}
