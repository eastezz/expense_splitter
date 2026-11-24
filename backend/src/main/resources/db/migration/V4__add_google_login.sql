-- Accounts can also come from "Sign in with Google" (step 12).
-- Such an account may have no password of its own, so password_hash becomes optional.
-- google_id is Google's permanent id for the person (the "sub" claim); one Google account = one of our accounts.

ALTER TABLE users ALTER COLUMN password_hash DROP NOT NULL;

ALTER TABLE users ADD COLUMN google_id VARCHAR(255);

ALTER TABLE users ADD CONSTRAINT uk_users_google_id UNIQUE (google_id);