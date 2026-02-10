create database ecopeace character set utf8 COLLATE utf8_bin;

USE ecopeace;

create user 'dbuser'@'%' IDENTIFIED BY 	'dbpass' ;


GRANT ALL PRIVILEGES ON *.* to 'dbuser'@'%'  ;


flush privileges;







CREATE TABLE ecobot_status (
    id INT AUTO_INCREMENT PRIMARY KEY,
    battery DOUBLE,
    current_angular_vel DOUBLE,
    depth_data INT,
    latitude DOUBLE,
    longitude DOUBLE,
    mppt_data TEXT, -- JSON 배열을 문자열로 저장
    mppt_status TEXT, -- JSON 배열을 문자열로 저장
    pump_values TEXT, -- JSON 배열을 문자열로 저장
    timestamp DATETIME,
    velocity DOUBLE,
    metadata_robotId VARCHAR(255),
    metadata_topic VARCHAR(255),
    updated_at DATETIME,
	create_at DATETIME
);




CREATE TABLE water_quality_data (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bg_ppb DOUBLE,
    chl_ug_l DOUBLE,
    current_state INT,
    hd0_mg_l DOUBLE,
    hd0_sat DOUBLE,
    latitude DOUBLE,
    longitude DOUBLE,
    ph_units DOUBLE,
    spcond_us_cm DOUBLE,
    temp_deg_c DOUBLE,
    timestamp DATETIME,
    turb_ntu DOUBLE
);

INSERT INTO user (id, create_at,create_by,update_at,update_by,email,image,login_id,name,password,phone) VALUES
     (1, NOW(),NULL,NOW(),NULL,NULL,NULL,'admin','admin','$2a$04$UiHe4ao12/LJLF4E8JgmVegSLSMa3dBEWWJuuFAeON.KEp/h2Bula',NULL);
INSERT INTO role (id, create_at,create_by,update_at,update_by,description,name,permission) VALUES (1, NOW(), 'admin',NOW(),'admin','admin','admin','{"menu": {"id": "root", "create" : true, "update": true, "delete": true, "view": true, "children": []}}');
INSERT INTO user_role (create_at,create_by,update_at,update_by,role_id,user_id) VALUES
     (NOW(),'admin',NOW(),'admin',1,1);