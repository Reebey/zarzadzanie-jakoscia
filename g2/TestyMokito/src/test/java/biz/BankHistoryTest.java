package biz;

import db.dao.DAO;
import model.Account;
import model.Operation;
import model.User;
import model.operations.LogIn;
import model.operations.LogOut;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankHistoryTest {
    private BankHistory target;

    @Mock
    private DAO dao;


    @BeforeEach
    void setUp() {
        target = new BankHistory(dao);
    }

    @Test
    void logLoginSuccess() throws SQLException {
        // GIVEN
        User user = new User();

        // WHEN
        target.logLoginSuccess(user);

        // THEN
        verify(dao, times(1)).logOperation(any(LogIn.class), eq(true));
    }

    @Test
    void logLoginFailure() throws SQLException {
        // GIVEN
        User user = null;
        String info = "Zła nazwa użytkownika testUser";

        // WHEN
        target.logLoginFailure(user, info);

        // THEN
        verify(dao, times(1)).logOperation(any(LogIn.class), eq(false));
    }

    @Test
    void logLogOut() throws SQLException {
        // GIVEN
        User user = new User();

        // WHEN
        target.logLogOut(user);

        // THEN
        verify(dao, times(1)).logOperation(any(LogOut.class), eq(true));
    }

    @Test
    void logPaymentIn() {
        // GIVEN
        Account account = new Account();
        double amount = 100.0;
        boolean success = true;

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> target.logPaymentIn(account, amount, success));
    }

    @Test
    void logPaymentOut() {
        // GIVEN
        Account account = new Account();
        double amount = 100.0;
        boolean success = true;

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> target.logPaymentOut(account, amount, success));
    }

    @Test
    void logOperation() throws SQLException {
        // GIVEN
        Operation operation = mock(Operation.class);
        boolean success = true;

        // WHEN
        target.logOperation(operation, success);

        // THEN
        verify(dao, times(1)).logOperation(operation, success);
    }

    @Test
    void logUnauthorizedOperation() {
        // GIVEN
        Operation operation = mock(Operation.class);
        boolean success = false;

        // WHEN & THEN
        assertThrows(RuntimeException.class, () -> target.logUnauthorizedOperation(operation, success));
    }
}