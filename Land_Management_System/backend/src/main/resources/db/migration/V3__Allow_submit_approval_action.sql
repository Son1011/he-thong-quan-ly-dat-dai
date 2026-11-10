-- Keep existing databases in sync with ApprovalAction.SUBMIT.
ALTER TABLE approval_history
DROP CHECK chk_approval_history_action;

ALTER TABLE approval_history
ADD CONSTRAINT chk_approval_history_action
CHECK (action_code IN (
    'CREATE',
    'APPROVE',
    'RETURN',
    'ESCALATE',
    'SUBMIT',
    'SEND_TO_CENTRAL'
));
