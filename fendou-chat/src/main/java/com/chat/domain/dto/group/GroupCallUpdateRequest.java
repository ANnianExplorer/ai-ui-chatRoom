package com.chat.domain.dto.group;

import lombok.Data;
import org.w3c.dom.Text;

/**
 * @author y
 * @since 2026-01-07
 */
@Data
public class GroupCallUpdateRequest {

    private Integer id;
    private String groupCall;
}
