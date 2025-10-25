package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    NAME_UPDATED_SUCCESSFULLY("✅ Name updated successfully!"),
    NAME_MUST_CONTAIN_TWO_WORDS("Name must contain two words with letters only"),

    SUCCESSFULLY_DEPOSITED("✅ Successfully deposited"),
    DEPOSIT_LESS_5000("❌ Please deposit less or equal to 5000$."),
    SUCCESSFULLY_TRANSFERRED("✅ Successfully transferred"),
    PLEASE_FILL_ALL_FIELDS_AND_CONFIRM("❌ Please fill all fields and confirm."),
    NO_USER_FOUND_WITH_ACCOUNT("❌ No user found with this account number."),
    ERROR_TRANSFER_AMOUNT_CANNOT_EXCEED_10000("❌ Error: Transfer amount cannot exceed 10000");
    private final String message;
    BankAlert(String message){
        this.message = message;
    }
}
