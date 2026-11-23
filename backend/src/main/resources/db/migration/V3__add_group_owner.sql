-- Every group gets an owner: the user account that created it (step 12).
-- Groups that were created before there were accounts have no owner (NULL).
-- V1 and V2 stay untouched; this file only adds to them.

ALTER TABLE expense_groups ADD COLUMN owner_id BIGINT REFERENCES users (id);

-- The start page asks "all groups of this user" on every visit; the index makes that fast.
CREATE INDEX idx_expense_groups_owner ON expense_groups (owner_id);