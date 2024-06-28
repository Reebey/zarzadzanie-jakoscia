package biz;

import db.dao.DAO;
import model.Account;
import model.Operation;
import model.User;
import model.operations.Interest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InterestOperatorTest {

    private InterestOperator target;

    @Mock
    private DAO dao;

    @Mock
    private AccountManager accountManager;

    @Mock
    private BankHistory bankHistory;

    @BeforeEach
    void setUp() {
        target = new InterestOperator(dao, accountManager);
        target.bankHistory = bankHistory;
    }

    @Test
    void countInterestForAccount() throws SQLException, SQLException {
        // GIVEN
        User user = mock(User.class);
        Account account = mock(Account.class);
        account.setOwner(user);
        int accountId = account.getId();
        when(account.getAmmount()).thenReturn(1000.0);
        when(dao.findUserByName("InterestOperator")).thenReturn(user);
        when(accountManager.paymentIn(eq(user), eq(200.0), eq("Interest ..."), eq(accountId))).thenReturn(true);

        // WHEN
        target.countInterestForAccount(account);

        // THEN
        verify(account, times(1)).getAmmount();
        verify(dao, times(1)).findUserByName("InterestOperator");
        verify(accountManager, times(1)).paymentIn(eq(user), eq(200.0), eq("Interest ..."), eq(accountId));
        verify(bankHistory, times(1)).logOperation(any(Interest.class), eq(true));
    }

    @Test
    void countInterestForAccountWhenPaymentFails() throws SQLException {
        // GIVEN
        User user = mock(User.class);
        Account account = mock(Account.class);
        account.setOwner(user);
        when(account.getAmmount()).thenReturn(1000.0);
        int accountId = account.getId();
        when(dao.findUserByName("InterestOperator")).thenReturn(user);
        when(accountManager.paymentIn(eq(user), eq(200.0), eq("Interest ..."), eq(accountId))).thenReturn(false);

        // WHEN
        target.countInterestForAccount(account);

        // THEN
        verify(account, times(1)).getAmmount();
        verify(dao, times(1)).findUserByName("InterestOperator");
        verify(accountManager, times(1)).paymentIn(eq(user), eq(200.0), eq("Interest ..."), eq(accountId));
        verify(bankHistory, times(1)).logOperation(any(Interest.class), eq(false));
    }

    @Test
    void countInterestForAccountSQLException() throws SQLException {
        // GIVEN
        Account account = mock(Account.class);
        when(account.getAmmount()).thenReturn(1000.0);
        when(dao.findUserByName("InterestOperator")).thenThrow(new SQLException());

        // WHEN & THEN
        assertThrows(SQLException.class, () -> target.countInterestForAccount(account));

        verify(account, times(1)).getAmmount();
        verify(dao, times(1)).findUserByName("InterestOperator");
        verify(accountManager, never()).paymentIn(any(User.class), anyDouble(), anyString(), anyInt());
        verify(bankHistory, never()).logOperation(any(Operation.class), anyBoolean());
    }
}