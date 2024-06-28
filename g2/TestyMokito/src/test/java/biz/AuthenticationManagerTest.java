package biz;

import db.dao.DAO;
import model.Operation;
import model.Password;
import model.Role;
import model.User;
import model.exceptions.UserUnnkownOrBadPasswordException;
import model.operations.OperationType;
import model.operations.Withdraw;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.InvocationTargetException;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationManagerTest {

    AuthenticationManager target;

    @Mock
    DAO mockDao;
    @Mock
    BankHistory mockHistory;

    @BeforeEach
    void setUp() {
        target = new AuthenticationManager(mockDao, mockHistory);
    }

    @Test
    void testLogInSuccess() throws SQLException, UserUnnkownOrBadPasswordException {
        // GIVEN
        String userName = "testUser";
        char[] password = "testPassword".toCharArray();
        User mockUser = mock(User.class);
        Password mockPassword = mock(Password.class);
        when(mockPassword.getPasswd()).thenReturn(AuthenticationManager.hashPassword(password.clone()));
        when(mockDao.findUserByName(userName)).thenReturn(mockUser);
        when(mockDao.findPasswordForUser(mockUser)).thenReturn(mockPassword);

        // WHEN
        User result = target.logIn(userName, password);

        // THEN
        assertEquals(mockUser, result);
        verify(mockHistory, times(1)).logLoginSuccess(mockUser);
    }

    @Test
    void testLogInUserNotFound() throws SQLException {
        // GIVEN
        String userName = "testUser";
        char[] password = "testPassword".toCharArray();
        when(mockDao.findUserByName(userName)).thenReturn(null);

        // WHEN / THEN
        assertThrows(UserUnnkownOrBadPasswordException.class, () -> target.logIn(userName, password));
        verify(mockHistory, times(1)).logLoginFailure(null, "Zła nazwa użytkownika " + userName);
    }

    @Test
    void testLogInBadPassword() throws SQLException {
        // GIVEN
        String userName = "testUser";
        char[] password = "testPassword".toCharArray();
        User mockUser = mock(User.class);
        Password mockPassword = mock(Password.class);
        when(mockDao.findUserByName(userName)).thenReturn(mockUser);
        when(mockDao.findPasswordForUser(mockUser)).thenReturn(mockPassword);
        when(mockPassword.getPasswd()).thenReturn("badHashedPassword");

        // WHEN / THEN
        assertThrows(UserUnnkownOrBadPasswordException.class, () -> target.logIn(userName, password));
        verify(mockHistory, times(1)).logLoginFailure(mockUser, "Bad Password");
    }

    @Test
    void testLogOut() throws SQLException {
        // GIVEN
        User mockUser = mock(User.class);

        // WHEN
        boolean result = target.logOut(mockUser);

        // THEN
        assertTrue(result);
        verify(mockHistory, times(1)).logLogOut(mockUser);
    }

    @Test
    void testCheckPassword() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // GIVEN
        String plainPassword = "testPassword";
        char[] password = plainPassword.toCharArray();
        Password passwordModel = new Password();
        passwordModel.setPasswd(AuthenticationManager.hashPassword(password));
        java.lang.reflect.Method checkPasswordMethod = AuthenticationManager.class.getDeclaredMethod("checkPassword", Password.class, char[].class);
        checkPasswordMethod.setAccessible(true);

        try {
            // WHEN
            boolean result = (boolean) checkPasswordMethod.invoke(target, passwordModel, plainPassword.toCharArray());

            // THEN
            assertTrue(result);
        } finally {
            checkPasswordMethod.setAccessible(false);
        }
    }

    @Test
    void testHashPassword() throws NoSuchAlgorithmException {
        // GIVEN
        String plainPassword = "testPassword";
        char[] password = plainPassword.toCharArray();

        // WHEN
        String hashedPassword = AuthenticationManager.hashPassword(password);

        // THEN
        byte[] bpass = Charset.forName("UTF-8").encode(CharBuffer.wrap(plainPassword.toCharArray())).array();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] encodedHash = digest.digest(bpass);
        String expectedHash = new String(Base64.encodeBase64(encodedHash));
        assertEquals(expectedHash, hashedPassword);
    }

    @Test
    void testHashPasswordNoSuchAlgorithmException() {
        // GIVEN
        String plainPassword = "testPassword";
        char[] password = plainPassword.toCharArray();

        try {
            java.lang.reflect.Method getInstanceMethod = MessageDigest.class.getDeclaredMethod("getInstance", String.class);
            getInstanceMethod.setAccessible(true);

            try (MockedStatic<MessageDigest> mockedStatic = mockStatic(MessageDigest.class)) {
                mockedStatic.when(() -> getInstanceMethod.invoke(null, "SHA-256"))
                        .thenThrow(new NoSuchAlgorithmException());

                // WHEN
                String hashedPassword = AuthenticationManager.hashPassword(password);

                // THEN
                assertNull(hashedPassword);
            }
        } catch (Exception e) {
            fail("Reflection setup failed: " + e.getMessage());
        }
    }

    @Test
    void testCanInvokeOperationAdmin() {
        // GIVEN
        Role mockRole = mock(Role.class);
        User mockUser = mock(User.class);
        Operation mockOperation = mock(Operation.class);
        when(mockRole.getName()).thenReturn("Admin");
        when(mockUser.getRole()).thenReturn(mockRole);

        // WHEN
        boolean result = target.canInvokeOperation(mockOperation, mockUser);

        // THEN
        assertTrue(result);
    }

    @Test
    void testCanInvokeOperationPaymentIn() {
        // GIVEN
        Role mockRole = mock(Role.class);
        User mockUser = mock(User.class);
        Operation mockOperation = mock(Operation.class);
        when(mockRole.getName()).thenReturn("User");
        when(mockUser.getRole()).thenReturn(mockRole);
        when(mockOperation.getType()).thenReturn(OperationType.PAYMENT_IN);

        // WHEN
        boolean result = target.canInvokeOperation(mockOperation, mockUser);

        // THEN
        assertTrue(result);
    }

    @Test
    void testCanInvokeOperationWithdraw() {
        // GIVEN
        Role mockRole = mock(Role.class);
        User mockUser = mock(User.class);
        Withdraw mockWithdraw = mock(Withdraw.class);
        when(mockRole.getName()).thenReturn("User");
        when(mockUser.getRole()).thenReturn(mockRole);
        when(mockWithdraw.getType()).thenReturn(OperationType.WITHDRAW);
        when(mockWithdraw.getUser()).thenReturn(mockUser);

        // WHEN
        boolean result = target.canInvokeOperation(mockWithdraw, mockUser);

        // THEN
        assertTrue(result);
    }

    @Test
    void testCannotInvokeOperation() {
        // GIVEN
        Role mockRole = mock(Role.class);
        User mockUser = mock(User.class);
        Operation mockOperation = mock(Operation.class);
        when(mockRole.getName()).thenReturn("User");
        when(mockUser.getRole()).thenReturn(mockRole);
        when(mockOperation.getType()).thenReturn(OperationType.LOG_OUT);

        // WHEN
        boolean result = target.canInvokeOperation(mockOperation, mockUser);

        // THEN
        assertFalse(result);
    }
}