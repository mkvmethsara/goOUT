package com.squadx.goout.Service;

import com.squadx.goout.Dto.Transfer;
import com.squadx.goout.Dto.UserBalance;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class SettlementService {

    public List<Transfer> calculateSettlements(List<UserBalance> balances) {

        List<Transfer> transfers = new ArrayList<>();

        // Separate users into two lists: those who OWE money, and those who are OWED money.
        List<UserBalance> debtors = new ArrayList<>();
        List<UserBalance> creditors = new ArrayList<>();

        for (UserBalance userBalance : balances) {
            int comparison = userBalance.getBalance().compareTo(BigDecimal.ZERO);
            if (comparison < 0) {
                // Balance is less than 0 (Negative)
                debtors.add(userBalance);
            } else if (comparison > 0) {
                // Balance is greater than 0 (Positive)
                creditors.add(userBalance);
            }
        }

        int debtorIndex = 0;
        int creditorIndex = 0;

        // Keep looping until we have gone through all debtors and creditors
        while (debtorIndex < debtors.size() && creditorIndex < creditors.size()) {
            UserBalance debtor = debtors.get(debtorIndex);
            UserBalance creditor = creditors.get(creditorIndex);

            // Get the absolute value of what the debtor owes (e.g., -500 becomes 500)
            BigDecimal debtAmount = debtor.getBalance().abs();
            BigDecimal creditAmount = creditor.getBalance();

            // The transfer amount is the smaller of the two values
            BigDecimal transferAmount = debtAmount.min(creditAmount);

            // Create the transfer instruction
            transfers.add(new Transfer(
                    debtor.getUserId(),
                    creditor.getUserId(),
                    transferAmount.setScale(2, RoundingMode.HALF_UP) // Round to 2 decimal places for currency
            ));

            // Adjust their remaining balances by subtracting the transferred amount
            debtor.setBalance(debtor.getBalance().add(transferAmount)); // adding positive to negative moves it towards 0
            creditor.setBalance(creditor.getBalance().subtract(transferAmount));

            // If the debtor's balance is now 0, move to the next debtor
            if (debtor.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                debtorIndex++;
            }

            // If the creditor's balance is now 0, move to the next creditor
            if (creditor.getBalance().compareTo(BigDecimal.ZERO) == 0) {
                creditorIndex++;
            }
        }

        return transfers;
    }
}