SET @admin_login_id = 'admin';
SET @admin_role_name = 'admin';

-- 관리자 계정 생성
INSERT INTO user (create_at, create_by, update_at, update_by, email, image, login_id, name, password, phone)
VALUES (NOW(), NULL, NOW(), NULL, NULL, NULL, @admin_login_id, @admin_login_id,
        '$2a$04$UiHe4ao12/LJLF4E8JgmVegSLSMa3dBEWWJuuFAeON.KEp/h2Bula', NULL);

-- 권한 생성
INSERT INTO role (create_at, create_by, update_at, update_by, description, name, permission)
VALUES (now(), null, now(), null, @admin_role_name, @admin_role_name,
        '{"menu":{"id":"root","children":[],"create":true,"update":true,"delete":true,"view":true}}');

-- 관리자 계정과 권한정보 연결
INSERT INTO user_role (create_at, create_by, update_at, update_by, role_id, user_id)
VALUES (now(), null, now(), null, (select id from role where name = @admin_role_name),
        (select id from user where login_id = @admin_login_id));

-- 다국어 카테고리 생성
INSERT INTO code_category (create_at, create_by, update_at, update_by, category, description, name)
VALUES (now(), 'system', now(), 'system', 'language', '다국어 코드 카테고리', '다국어');

-- 다국어 코드 생성
INSERT INTO `code` (create_at, create_by, update_at, update_by, code, description, name, sort, value, category,
                    child_category)
VALUES (now(), 'system', now(), 'system', 'ko', null, '한국어', 1, null, 'language', null),
       (now(), 'system', now(), 'system', 'en', null, '영어', 2, null, 'language', null);

-- 시스템 설정 데이터 생성
INSERT INTO system_info (system_key, value, large_value, create_at, create_by, update_at)
VALUES ('title', 'Smart Factory Platform', null, now(), 'system', now()),
       ('logo', null,
        'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMzAiIGhlaWdodD0iMzAiIHZpZXdCb3g9IjAgMCAzMCAzMCIgZmlsbD0ibm9uZSIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj4NCjxnIGNsaXAtcGF0aD0idXJsKCNjbGlwMF8yMTRfNjcyKSI+DQo8cGF0aCBkPSJNMTEuNDY1NSAyLjkwNDk1QzEwLjI4MzcgNC4wMDI3NCA4LjkxOTAzIDUuNDI5ODYgNy41NjA3OSA3LjE4NjMyQzEwLjU3ODYgMy45NzkwNiAxMy4xNDIzIDcuMDAzMzUgMTUuMDAyMSA5LjI1Mjc0QzE2Ljg0NDYgMTEuMDk1MyAxOS44IDE1LjA1MzggMTkuOCAxNy43MDM1QzE5LjggMTguODIyOCAxOS40MTY5IDE5Ljg0OTYgMTguNzczMyAyMC42Njc2QzIwLjk3MzIgMTguNDgwNiAyNS40Mjg5IDEzLjMxNDUgMjMuNzU0MiA5LjAwMzA0QzIxLjk4NyA2LjM4NzczIDIwLjEwMTQgNC4zNjAwNiAxOC41MzY1IDIuOTA0OTVDMTYuNTQ1NCAxLjA1NTkzIDEzLjQ1ODcgMS4wNTU5MyAxMS40Njc2IDIuOTA0OTVIMTEuNDY1NVoiIGZpbGw9InVybCgjcGFpbnQwX3JhZGlhbF8yMTRfNjcyKSIvPg0KPHBhdGggZD0iTTI1LjMzNDMgMjYuOTI0OUMyNy45MzI0IDI2LjEyNDIgMjkuNDc1NyAyMy40NTI5IDI4Ljg2ODcgMjAuODAzMkMyOC4yNzI1IDE4LjE5NjUgMjcuMTUzMiAxNC43MTM3IDI1LjA1MjMgMTEuMDc1OUMyNC42MzI2IDEwLjM0ODQgMjQuMTk1NiA5LjY1NzQyIDIzLjc1NDMgOS4wMDMwNUMyNS40MjkgMTMuMzE2NyAyMC45NzMzIDE4LjQ4MjggMTguNzczNCAyMC42Njc2QzE3Ljg5NTIgMjEuNzg0NyAxNi41MzI2IDIyLjUwMTUgMTUgMjIuNTAxNUMxMi40OTIzIDIyLjUwMTUgMTAuNDM0NSAyMC41NzcyIDEwLjIyMTQgMTguMTIzM0MxMC4yMyAyMS4xOTA2IDExLjM4ODEgMjcuMzg3NyAxNy4xODI3IDI4LjQxMjNDMjAuNDQzOCAyOC4yMDU3IDIzLjIzMzQgMjcuNTcwNyAyNS4zMzQzIDI2LjkyNDlaIiBmaWxsPSJ1cmwoI3BhaW50MV9yYWRpYWxfMjE0XzY3MikiLz4NCjxwYXRoIGQ9Ik0xMC4yMjE0IDE4LjEyMzNDMTAuMjA4NSAxNy45ODU2IDEwLjIwMiAxNy44NDU2IDEwLjIwMiAxNy43MDM2QzEwLjIwMiAxNS4wNTM4IDEzLjE1NzQgMTEuMDk1MyAxNSA5LjI1Mjc4QzEzLjE0MjQgNy4wMDEyNSAxMC41NzY1IDMuOTc5MTEgNy41NTg3MSA3LjE4NjM2QzYuNjY3NTcgOC4zMzc5NiA1Ljc4MDczIDkuNjMxNjMgNC45NDc3IDExLjA3MzhDMi44NDY4NCAxNC43MTM3IDEuNzI3NTMgMTguMTk2NSAxLjEzMTI4IDIwLjgwMzJDMC41MjQyNzIgMjMuNDUzIDIuMDY3NjMgMjYuMTI2NCA0LjY2NTcyIDI2LjkyNUM3LjIyMDc3IDI3LjcxMjggMTAuNzk4MyAyOC40ODM0IDE1IDI4LjQ4MzRDMTUuNzQ5MSAyOC40ODM0IDE2LjQ3NjYgMjguNDU3NiAxNy4xODI2IDI4LjQxMjRDMTEuMzg4IDI3LjM4NzggMTAuMjMgMjEuMTkwNyAxMC4yMjE0IDE4LjEyMzNaIiBmaWxsPSJ1cmwoI3BhaW50Ml9yYWRpYWxfMjE0XzY3MikiLz4NCjwvZz4NCjxkZWZzPg0KPHJhZGlhbEdyYWRpZW50IGlkPSJwYWludDBfcmFkaWFsXzIxNF82NzIiIGN4PSIwIiBjeT0iMCIgcj0iMSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiIGdyYWRpZW50VHJhbnNmb3JtPSJ0cmFuc2xhdGUoMjAuMDExIDE2Ljk5NzUpIHNjYWxlKDE3LjAwMjgpIj4NCjxzdG9wIHN0b3AtY29sb3I9IiM0RDY2RDIiLz4NCjxzdG9wIG9mZnNldD0iMC40OSIgc3RvcC1jb2xvcj0iIzREOUZEMiIvPg0KPHN0b3Agb2Zmc2V0PSIxIiBzdG9wLWNvbG9yPSIjMDRDMEI1Ii8+DQo8L3JhZGlhbEdyYWRpZW50Pg0KPHJhZGlhbEdyYWRpZW50IGlkPSJwYWludDFfcmFkaWFsXzIxNF82NzIiIGN4PSIwIiBjeT0iMCIgcj0iMSIgZ3JhZGllbnRVbml0cz0idXNlclNwYWNlT25Vc2UiIGdyYWRpZW50VHJhbnNmb3JtPSJ0cmFuc2xhdGUoOS41OTkzNiAyMi4xMTg0KSBzY2FsZSgxOC4zODI1KSI+DQo8c3RvcCBzdG9wLWNvbG9yPSIjNEQ2NkQyIi8+DQo8c3RvcCBvZmZzZXQ9IjAuNDkiIHN0b3AtY29sb3I9IiM0RDlGRDIiLz4NCjxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iIzA0QzBCNSIvPg0KPC9yYWRpYWxHcmFkaWVudD4NCjxyYWRpYWxHcmFkaWVudCBpZD0icGFpbnQyX3JhZGlhbF8yMTRfNjcyIiBjeD0iMCIgY3k9IjAiIHI9IjEiIGdyYWRpZW50VW5pdHM9InVzZXJTcGFjZU9uVXNlIiBncmFkaWVudFRyYW5zZm9ybT0idHJhbnNsYXRlKDEzLjA0OTggOC4wNzc1MSkgc2NhbGUoMjIuODUxMiAyMi44NTEyKSI+DQo8c3RvcCBzdG9wLWNvbG9yPSIjNEQ2NkQyIi8+DQo8c3RvcCBvZmZzZXQ9IjAuNDkiIHN0b3AtY29sb3I9IiM0RDlGRDIiLz4NCjxzdG9wIG9mZnNldD0iMSIgc3RvcC1jb2xvcj0iIzA0QzBCNSIvPg0KPC9yYWRpYWxHcmFkaWVudD4NCjxjbGlwUGF0aCBpZD0iY2xpcDBfMjE0XzY3MiI+DQo8cmVjdCB3aWR0aD0iMjgiIGhlaWdodD0iMjYuOTY2OCIgZmlsbD0id2hpdGUiIHRyYW5zZm9ybT0idHJhbnNsYXRlKDEgMS41MTY2KSIvPg0KPC9jbGlwUGF0aD4NCjwvZGVmcz4NCjwvc3ZnPg0K',
        now(), 'system', now());


-- APC 데이터 생성
-- APC 시스템 이용시에만 생성하면 됩니다
INSERT INTO ecopeace.code_category (create_at, create_by, update_at, update_by, category, description, name) VALUES (now(), 'system', now(), 'system', 'APC_CONDITIONS', null, 'APC_ 조건');
INSERT INTO ecopeace.code_category (create_at, create_by, update_at, update_by, category, description, name) VALUES ( now(), 'system', now(), 'system', 'APC_DATA_PARAMETERS', null, 'APC 데이터 파라미터');
