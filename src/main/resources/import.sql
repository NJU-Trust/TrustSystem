insert into user (username, password, credit_score) values ('test', '$2a$10$3lj/cEZUJiepqAwekmiaKe9p6X7gudLsc9jiZixB.53PStsrdS7pS', 85);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (1, 1000, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (2, 1050, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (3, 1010, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (4, 900, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (5, 890, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (6, 1001, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (7, 1023, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (8, 1045, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (9, 1567, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (10, 1234, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (11, 1111, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (12, 1243, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (13, 1456, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (14, 1325, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (15, 1456, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (16, 1346, 500, 500, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (17, 908, 500, 400, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (18, 1432, 500, 300, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (19, 1200, 500, 400, 'test', 100);
insert into user_month_statistics(id, income, disc, surplus, username, debt) values (60, 1000, 500, 501, 'test', 100);
insert into user (username, password, credit_score, roles) values ('admin', '$2a$10$3lj/cEZUJiepqAwekmiaKe9p6X7gudLsc9jiZixB.53PStsrdS7pS', 0, 'ADMIN');
insert into base_target(id, username, target_type, collected_money, money, project_description, start_time, target_state, target_rating) values (1, 'test', 'SMALL', 200, 500, 'description', '2018-09-10', 'ON_GOING', 'A');
insert into repayment(id, username, interest_rate, target_id, duration, type, start_date) values (1, 'test', 0.5, 1, 2, 'EQUAL_PRINCIPAL', '2018-09-01');
insert into small_target(id, classification) VALUES (1, 0);
INSERT INTO user_info_check_record(id, check_item, check_state, description, message, time, username) VALUES ('1', 'VOLUNTEERTIME', 'UPDATE', 'description', 'message', '2018-09-13', 'test');
INSERT INTO base_user_evidence(data_type, id, evidence, state, time, item, username) VALUES ('VOLUNTEERTIME', '1', 'evidence1', 'UPDATE', '2018-09-13', '1', 'test');
INSERT INTO base_user_evidence(data_type, id, evidence, state, time, item, username) VALUES ('VOLUNTEERTIME', '2', 'evidence2', 'ONGING', '2018-09-13', '1', 'test');
INSERT INTO volunteer_evidence(length, id) VALUES ('1', '1');
INSERT INTO volunteer_evidence(length, id) VALUES ('2', '2');
INSERT INTO unstructured_data(id, data_type, score, username) VALUES (1, 0, 100, 'test');
INSERT INTO repayment(id, difficulty, duration, interest_rate, remaining_amount, start_date, total_interest, type, target_id, username) VALUES (1, 50.0, 1, 0.5, 100, 2018-09-10, 10, EQUAL_PRINCIPAL, 1, 'test');
