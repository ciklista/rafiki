import axios from 'axios';

const PROMETHEUS_PORT = ':30090';
const PROMETHEUS_BASEPATH = '/api/v1/query?query=';

export function getOperatorsRecordsOut(ip: string, jobId: string): Promise<any> {
    const query = `sum by (job_id, task_name) (flink_taskmanager_job_task_numRecordsOutPerSecond {job_id='${jobId}'})`;
    return axios.get(ip + PROMETHEUS_PORT + PROMETHEUS_BASEPATH + query
    );
}

export function getOperatorRecordsIn(ip: string, jobId: string, operatorName: string): Promise<any> {
    const query = `sum by (job_id, task_name) (flink_taskmanager_job_task_numRecordsInPerSecond {job_id='${jobId}', task_name='${operatorName}'})`;
    return axios.get(ip + PROMETHEUS_PORT + PROMETHEUS_BASEPATH + query
    );
}
