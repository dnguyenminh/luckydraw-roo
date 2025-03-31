package vn.com.fecredit.app.dto;

/**
 * Filter request for table data.
 */
public class FilterRequest {
    private String field;
    private String operator;
    private String value;

    // Getters and setters
    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
