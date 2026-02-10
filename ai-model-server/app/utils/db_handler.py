import pymysql
import configparser
import os
from datetime import datetime
from io import BytesIO
from dataclasses import dataclass
from typing import Optional, List
import json
import gzip

''' AI MODEL 테이블 스키마
create table ai_model
(
    id                bigint auto_increment primary key,
    create_at         datetime(6)   not null,
    create_by         varchar(255)  null,
    update_at         datetime(6)   null,
    update_by         varchar(255)  null,
    algorithm         varchar(255)  not null,
    description       varchar(255)  null,
    from_date         datetime(6)   null,
    name              varchar(255)  not null,
    parameter_details varchar(1000) null, // json string ( ex. [{id: 111, name: 'P1'}, {id: 222, name: 'P2}] )
    status            varchar(50)   null,
    to_date           datetime(6)   null,
    tool_id           bigint        null,
    tool_name         varchar(255)  null,
    y_parameter_id    bigint        null
);
'''

''' parameter_data 스키마
create table parameter_data
(
    id                 bigint auto_increment
        primary key,
    create_at          datetime(6)    not null,
    create_by          varchar(255)   null,
    update_at          datetime(6)    null,
    update_by          varchar(255)   null,
    d_value            decimal(15, 5) null,
    data_type          varchar(20)    null,
    i_value            int            null,
    image_value        varchar(500)   null,
    is_ctrl_limit_over varchar(1)     null comment 'Ctrl Limit Over 여부'
        check (`is_ctrl_limit_over` in ('N', 'Y')),
    is_spec_limit_over varchar(1)     null comment 'Spec Limit Over 여부'
        check (`is_spec_limit_over` in ('N', 'Y')),
    latitude_value     double         null comment '위도 데이터 값',
    lcl                decimal(15, 5) null,
    longitude_value    double         null comment '경도 데이터 값',
    lsl                decimal(15, 5) null,
    parameter_id       bigint         null,
    s_value            varchar(255)   null,
    target             decimal(15, 5) null,
    trace_at           datetime(6)    null,
    ucl                decimal(15, 5) null,
    usl                decimal(15, 5) null
);

create index parameter_data_search_complex_index
    on parameter_data (parameter_id, trace_at);
'''

'''
create table process_optimization
(
    id                      bigint auto_increment        primary key,
    create_at               datetime(6)                          not null,
    create_by               varchar(255)                         null,
    update_at               datetime(6)                          null,
    update_by               varchar(255)                         null,
    complete_count          double                               null,
    name                    varchar(255)                         null,
    optimal_value           double                               null,
    status                  enum ('SUCCESS', 'FAIL', 'STUDYING') null,
    target_value            double                               not null,
    tool_id                 bigint                               null,
    total_count             double                               null,
    ai_model_id             bigint                               null,
    optimization_parameters varchar(255)                         null,
    constraint FKlajf4ihawcahyeswpcd985tta
        foreign key (ai_model_id) references ai_model (id)
);
'''

'''
create table process_optimization_analysis
(
    id                      bigint auto_increment primary key,
    create_at               datetime(6)  not null,
    create_by               varchar(255) null,
    update_at               datetime(6)  null,
    update_by               varchar(255) null,
    analysis_data           longblob     null,
    process_optimization_id bigint       null,
    constraint UK_l19lo5jcoal8c7066o70xv385
        unique (process_optimization_id),
    constraint FK44nwa8prth663uj4r36id09j
        foreign key (process_optimization_id) references process_optimization (id)
);
'''

class DBHandler:
    def __init__(self, site):
        current_dir = os.path.dirname(os.path.abspath(__file__))
        config_path = os.path.abspath(os.path.join(current_dir, ".."))

        config = configparser.ConfigParser()
        config.read(config_path + os.path.sep + 'config.ini', encoding='UTF-8')

        config_name = 'DB_' + site
        db_config = config[config_name]

        self.db_name = db_config['dbname']
        self.host = db_config['host']
        self.port = db_config['port']
        self.user = db_config['user']
        self.pw = db_config['pw']

    def __get_connection(self):
        conn = pymysql.connect(
            user=self.user,
            passwd=self.pw,
            host=self.host,
            db=self.db_name,
            port=int(self.port),
            charset='utf8'
        )
        curs = conn.cursor(pymysql.cursors.DictCursor)
        return conn, curs
    

    def select_model_by_id(self, model_id):
        """
        고유 ID로 ai_model 테이블에서 모델을 조회합니다.
        
        매개변수:
            model_id: 조회할 모델의 고유 식별자
        
        반환:
            AIModel 객체 또는 데이터가 없으면 None.
        """
        conn, curs = self.__get_connection()
        try:
            sql = "SELECT * FROM ai_model WHERE id = %s"
            curs.execute(sql, (model_id,))
            row = curs.fetchone()
            if row:
                return AIModel.from_dict(row)
            return None
        finally:
            conn.close()

    def select_parameter_data(self, parameter_id, from_date, to_date):
        """
        파라미터ID, from, to를 인자로 받아서 파라미터 데이터를 조회한다.
        날짜를 조회하는 대상 컬럼은 trace_at,
        아래의 필드만 조회한다:
         - id, trace_at, d_value, i_value, data_type

        반환:
            조회된 파라미터 데이터를 담은 ParameterData 객체들의 리스트.
        """
        conn, curs = self.__get_connection()
        try:
            sql = """
                SELECT id, trace_at, d_value, i_value, data_type
                FROM parameter_data
                WHERE parameter_id = %s
                  AND trace_at BETWEEN %s AND %s
            """
            curs.execute(sql, (parameter_id, from_date, to_date))
            rows = curs.fetchall()
            return [ParameterData.from_dict(row) for row in rows]
        finally:
            conn.close()

    def update_model_complete_status(self, model_id):
        conn, curs = self.__get_connection()
        try:
            sql = """
            UPDATE ai_model
                SET status = 'COMPLETE'
                    , update_at = now()
                WHERE id = %s
            """
            curs.execute(sql, (model_id))
            conn.commit()
        finally:
            conn.close()

    def update_model_build_fail_status(self, model_id, failure_reason):
        conn, curs = self.__get_connection()
        try:
            sql = """
            UPDATE ai_model
                SET status = 'FAIL'
                    , failure_reason = %s
                    , update_at = now()
                WHERE id = %s
            """
            curs.execute(sql, (failure_reason, model_id))
            conn.commit()
        finally:
            conn.close()

    def select_optimization_info(self, optimization_id):
        """
        process_optimization 테이블에서 데이터를 조회합니다.

        매개변수:
            optimization_id: 조회할 최적화 정보의 고유 식별자

        반환:
            ProcessOptimization 객체 또는 데이터가 없으면 None.
        """
        conn, curs = self.__get_connection()
        try:
            sql = "SELECT * FROM process_optimization WHERE id = %s"
            curs.execute(sql, (optimization_id,))
            row = curs.fetchone()
            if row:
                return ProcessOptimization.from_dict(row)
            return None
        finally:
            conn.close()

    def update_optimization_start_status(self, optimization_id, combination_size):
        conn, curs = self.__get_connection()
        try:
            sql = """
            UPDATE process_optimization
                SET total_count = %s,
                    complete_count = 0,
                    status = 'STUDYING',
                    update_at = now()
                WHERE id = %s
            """
            curs.execute(sql, (combination_size, optimization_id))
            conn.commit()
        finally:
            conn.close()

    def update_optimization_complete_count(self, optimization_id, complete_count):
        conn, curs = self.__get_connection()
        try:
            sql = """
            UPDATE process_optimization
                SET complete_count = %s,
                    update_at = now()
                WHERE id = %s
            """
            curs.execute(sql, (complete_count, optimization_id))
            conn.commit()
        finally:
            conn.close()

    def update_optimization_success(self, optimization_id, complete_count, optimal_value, simulation_result):
        conn, curs = self.__get_connection()
        try:
            sql = """
            UPDATE process_optimization
                SET complete_count = %s,
                    optimal_value = %s,
                    status = 'SUCCESS',
                    update_at = now()
                WHERE id = %s
            """
            curs.execute(sql, (complete_count, optimal_value, optimization_id))

            # 시뮬레이션 결과를 gzip으로 저장
            compressed_data = BytesIO()
            with gzip.GzipFile(fileobj=compressed_data, mode='wb') as gz:
                gz.write(json.dumps(simulation_result).encode('utf-8'))

            sql_analysis = """
                INSERT INTO process_optimization_analysis (analysis_data, process_optimization_id, create_at)
                    VALUES (%s, %s, now())
            """
            curs.execute(sql_analysis, (compressed_data.getvalue(), optimization_id))

            conn.commit()
        finally:
            conn.close()

@dataclass
class AIModel:
    id: int
    create_at: datetime
    create_by: Optional[str] = None
    update_at: Optional[datetime] = None
    update_by: Optional[str] = None
    algorithm: str = ''
    description: Optional[str] = None
    from_date: Optional[datetime] = None
    name: str = ''
    parameter_details: Optional[str] = None
    status: Optional[str] = None
    to_date: Optional[datetime] = None
    tool_id: Optional[int] = None
    tool_name: Optional[str] = None
    y_parameter_id: Optional[int] = None

    def get_x_parameter_ids(self):
        if not self.parameter_details:
            return []
        details_list = json.loads(self.parameter_details)
        return [item['id'] for item in details_list if 'id' in item]

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            id=data.get('id'),
            create_at=data.get('create_at'),
            create_by=data.get('create_by'),
            update_at=data.get('update_at'),
            update_by=data.get('update_by'),
            algorithm=data.get('algorithm'),
            description=data.get('description'),
            from_date=data.get('from_date'),
            name=data.get('name'),
            parameter_details=data.get('parameter_details'),
            status=data.get('status'),
            to_date=data.get('to_date'),
            tool_id=data.get('tool_id'),
            tool_name=data.get('tool_name'),
            y_parameter_id=data.get('y_parameter_id')
        )
    

@dataclass
class ParameterData:
    id: int
    trace_at: datetime
    d_value: Optional[float] = None
    i_value: Optional[int] = None
    data_type: Optional[str] = None

    def get_value(self):
        if self.data_type == "DOUBLE":
            return self.d_value
        elif self.data_type == "INTEGER":
            return self.i_value
        return None

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            id=data.get('id'),
            trace_at=data.get('trace_at'),
            d_value=data.get('d_value'),
            i_value=data.get('i_value'),
            data_type=data.get('data_type')
        )
    

@dataclass
class OptimizationParameter:
    parameter_id: str
    min_scale_x: float
    max_scale_x: float
    step: int

    @classmethod
    def from_dict(cls, data: dict):
        return cls(
            parameter_id = data.get('parameterId'),
            min_scale_x = data.get('minScaleX'),
            max_scale_x = data.get('maxScaleX'),
            step = data.get('step')
        )

@dataclass
class ProcessOptimization:
    id: int
    create_at: datetime
    create_by: Optional[str] = None
    update_at: Optional[datetime] = None
    update_by: Optional[str] = None
    complete_count: Optional[float] = None
    name: Optional[str] = None
    optimal_value: Optional[float] = None
    status: Optional[str] = None  # Enum field as string
    target_value: float = 0.0
    tool_id: Optional[int] = None
    total_count: Optional[float] = None
    ai_model_id: Optional[int] = None
    optimization_parameters: Optional[List[OptimizationParameter]] = None

    @classmethod
    def from_dict(cls, data: dict):
        _optimization_parameters = None
        if data.get('optimization_parameters') is not None:
            _optimization_parameters = []
            items = json.loads(data.get('optimization_parameters'))
            for item in items:
                _optimization_parameters.append(OptimizationParameter.from_dict(item))

        return cls(
            id=data.get('id'),
            create_at=data.get('create_at'),
            create_by=data.get('create_by'),
            update_at=data.get('update_at'),
            update_by=data.get('update_by'),
            complete_count=data.get('complete_count'),
            name=data.get('name'),
            optimal_value=data.get('optimal_value'),
            status=data.get('status'),
            target_value=data.get('target_value'),
            tool_id=data.get('tool_id'),
            total_count=data.get('total_count'),
            ai_model_id=data.get('ai_model_id'),
            optimization_parameters=_optimization_parameters
        )

