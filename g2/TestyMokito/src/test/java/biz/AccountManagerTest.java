package biz;

import db.dao.DAO;
import db.dao.impl.SQLiteDB;
import model.Account;
import model.Operation;
import model.User;
import model.exceptions.OperationIsNotAllowedException;
import model.exceptions.UserUnnkownOrBadPasswordException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.verification.VerificationMode;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class AccountManagerTest {

    AccountManager target;
    @Mock
    DAO mockDao;
    @Mock
    BankHistory mockHistory; // = Mockito.mock(BankHistory.class);
    @Mock
    AuthenticationManager mockAuthManager;
    @Mock
    InterestOperator mockIntOperator;
    @Mock
    User mockUser;

    @BeforeEach
    void setUp() {
        target = new AccountManager();
        target.dao = mockDao;
        target.history=mockHistory;
        target.auth = mockAuthManager;
        target.interestOperator = mockIntOperator;
        target.loggedUser = mockUser;
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void paymentIn() throws SQLException {
        // GIVEN
        int accId = 13;
        User user = new User();
        Account a = mock(Account.class);
        String desc = "Wpłata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(a);
        when(mockDao.updateAccountState(eq(a))).thenReturn(true);

        // WHEN
        boolean result = target.paymentIn(user, amount, desc, accId);

        // THEN
        assertTrue(result);
        verify(a, times(1)).income(amount);
        verify(mockDao, times(1)).findAccountById(eq(accId));
        verify(mockDao, times(1)).updateAccountState(eq(a));
        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class), eq(true));
    }

    @Test
    void nullAccountpaymentIn() throws SQLException {
        // GIVEN
        int accId = 13;
        User user = new User();
        String desc = "Wpłata";
        double amount = 123;
        when(mockDao.findAccountById(anyInt())).thenReturn(null);

        // WHEN
        boolean result = target.paymentIn(user, amount, desc, accId);

        // THEN
        assertFalse(result);
    }

    @Test
    void paymentOut() throws SQLException, OperationIsNotAllowedException {
        // GIVEN
        Account mockSourceAccount = mock(Account.class);
        int accId = 13;
        String desc = "Wypłata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(mockSourceAccount);
        when(mockAuthManager.canInvokeOperation(any(Operation.class), eq(mockUser))).thenReturn(true);
        when(mockDao.updateAccountState(eq(mockSourceAccount))).thenReturn(true);

        // WHEN
        boolean result = target.paymentOut(mockUser, amount, desc, accId);

        // THEN
        assertTrue(result);
        verify(mockSourceAccount, times(1)).outcome(amount);
        verify(mockDao, times(1)).findAccountById(eq(accId));
        verify(mockDao, times(1)).updateAccountState(eq(mockSourceAccount));
        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class), eq(true));
    }

    @Test
    void unauthorizedPaymentOut() throws SQLException, OperationIsNotAllowedException {
        // GIVEN
        Account mockSourceAccount = mock(Account.class);
        int accId = 13;
        String desc = "Wypłata";
        double amount = 123;
        when(mockDao.findAccountById(eq(accId))).thenReturn(mockSourceAccount);
        when(mockAuthManager.canInvokeOperation(any(Operation.class), eq(mockUser))).thenReturn(false);

        // WHEN & THEN
        assertThrows(OperationIsNotAllowedException.class, () -> {
            target.paymentOut(mockUser, amount, desc, accId);
        });
        verify(mockHistory, atLeastOnce()).logUnauthorizedOperation(any(Operation.class), eq(false));
    }

    @Test
    void internalPayment() throws SQLException, OperationIsNotAllowedException {
        // GIVEN
        Account mockSourceAccount = mock(Account.class);
        Account mockDestAccount = mock(Account.class);
        int sourceAccId = 13;
        int destAccId = 14;
        String desc = "Przelew";
        double amount = 123;
        when(mockDao.findAccountById(eq(sourceAccId))).thenReturn(mockSourceAccount);
        when(mockDao.findAccountById(eq(destAccId))).thenReturn(mockDestAccount);
        when(mockAuthManager.canInvokeOperation(any(Operation.class), eq(mockUser))).thenReturn(true);
        when(mockDao.updateAccountState(eq(mockSourceAccount))).thenReturn(true);
        when(mockDao.updateAccountState(eq(mockDestAccount))).thenReturn(true);
        when(mockSourceAccount.outcome(amount)).thenReturn(true);
        when(mockDestAccount.income(amount)).thenReturn(true);

        // WHEN
        boolean result = target.internalPayment(mockUser, amount, desc, sourceAccId, destAccId);

        // THEN
        assertTrue(result);
        verify(mockSourceAccount, times(1)).outcome(amount);
        verify(mockDestAccount, times(1)).income(amount);
        verify(mockDao, times(2)).findAccountById(anyInt());
        verify(mockDao, times(2)).updateAccountState(any(Account.class));
        verify(mockHistory, atLeastOnce()).logOperation(any(Operation.class), eq(true));
    }

    @Test
    void unauthorizedInternalPayment() throws SQLException {
        // GIVEN
        Account mockSourceAccount = mock(Account.class);
        Account mockDestAccount = mock(Account.class);
        int sourceAccId = 13;
        int destAccId = 14;
        String desc = "Przelew";
        double amount = 123;
        when(mockDao.findAccountById(eq(sourceAccId))).thenReturn(mockSourceAccount);
        when(mockDao.findAccountById(eq(destAccId))).thenReturn(mockDestAccount);
        when(mockAuthManager.canInvokeOperation(any(Operation.class), eq(mockUser))).thenReturn(false);

        // WHEN & THEN
        assertThrows(OperationIsNotAllowedException.class, () -> {
            target.internalPayment(mockUser, amount, desc, sourceAccId, destAccId);
        });
        verify(mockHistory, atLeastOnce()).logUnauthorizedOperation(any(Operation.class), eq(false));
    }

    @Test
    void logInSuccess() throws SQLException, UserUnnkownOrBadPasswordException {
        // THEN
        String userName = "user";
        char[] password = "password".toCharArray();
        when(mockAuthManager.logIn(eq(userName), eq(password))).thenReturn(mockUser);

        // WHEN
        boolean result = target.logIn(userName, password);

        // THEN
        assertTrue(result);
        assertEquals(mockUser, target.getLoggedUser());
    }

    @Test
    void logInFailure() throws SQLException, UserUnnkownOrBadPasswordException {
        // GIVEN
        String userName = "user";
        char[] password = "password".toCharArray();
        when(mockAuthManager.logIn(eq(userName), eq(password))).thenReturn(null);

        // WHEN
        boolean result = target.logIn(userName, password);

        // THEN
        assertFalse(result);
        assertNull(target.getLoggedUser());
    }

    @Test
    void logOutSuccess() throws SQLException {
        // GIVEN
        when(mockAuthManager.logOut(eq(mockUser))).thenReturn(true);

        // WHEN
        boolean result = target.logOut(mockUser);

        // THEN
        assertTrue(result);
        assertNull(target.getLoggedUser());
    }

    @Test
    void logOutFailure() throws SQLException {
        // GIVEN
        when(mockAuthManager.logOut(eq(mockUser))).thenReturn(false);

        // WHEN
        boolean result = target.logOut(mockUser);

        // THEN
        assertFalse(result);
    }

    @Test
    void buildBankSuccess() throws SQLException, ClassNotFoundException {
        // GIVEN
        try (MockedStatic<SQLiteDB> mockedStatic = mockStatic(SQLiteDB.class)) {
            mockedStatic.when(SQLiteDB::createDAO).thenReturn(mockDao);

            // WHEN
            AccountManager bank = AccountManager.buildBank();

            // THEN
            assertNotNull(bank);
            assertNotNull(bank.dao);
            assertNotNull(bank.history);
            assertNotNull(bank.auth);
            assertNotNull(bank.interestOperator);
        }
    }

    @Test
    void buildBankFailureDueToSQLException() {
        // GIVEN
        try (MockedStatic<SQLiteDB> mockedStatic = mockStatic(SQLiteDB.class)) {
            mockedStatic.when(SQLiteDB::createDAO).thenThrow(SQLException.class);

            // WHEN
            AccountManager bank = AccountManager.buildBank();

            // THEN
            assertNull(bank);
        }
    }

    @Test
    void buildBankFailureDueToClassNotFoundException() {
        // GIVEN
        try (MockedStatic<SQLiteDB> mockedStatic = mockStatic(SQLiteDB.class)) {
            mockedStatic.when(SQLiteDB::createDAO).thenThrow(ClassNotFoundException.class);

            // WHEN
            AccountManager bank = AccountManager.buildBank();

            // THEN
            assertNull(bank);
        }
    }

    @Test
    void getLoggedUserTest() {
        // GIVEN
        target.loggedUser = mockUser;

        // WHEN
        User result = target.getLoggedUser();

        // THEN
        assertEquals(mockUser, result);
    }
}