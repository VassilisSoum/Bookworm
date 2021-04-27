CREATE TABLE IF NOT EXISTS BOOKWORM.EMAIL_TEMPLATE
(
    name    TEXT NOT NULL PRIMARY KEY,
    subject TEXT NOT NULL,
    body    TEXT NOT NULL
);

INSERT INTO BOOKWORM.EMAIL_TEMPLATE(name, subject, body)
VALUES ('registration-verification-pending-template', 'Welcome to Bookworm',
        'Welcome {CustomerFirstNameField} {CustomerLastNameField}. To complete the registration copy and paste the security code: {RegistrationVerificationToken}');