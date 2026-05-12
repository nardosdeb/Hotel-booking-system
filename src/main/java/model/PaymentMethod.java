package model;

public class PaymentMethod {
    private int methodId;
    private String methodName;
    private String accountDetails;

    public PaymentMethod() {}

    public PaymentMethod(String methodName, String accountDetails) {
        this.methodName = methodName;
        this.accountDetails = accountDetails;
    }

    // Getters and Setters
    public int getMethodId() { return methodId; }
    public void setMethodId(int methodId) { this.methodId = methodId; }

    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }

    public String getAccountDetails() { return accountDetails; }
    public void setAccountDetails(String accountDetails) { this.accountDetails = accountDetails; }
}