export interface PrometheusData {
    result: PrometheusResult[]
}

export interface PrometheusResult {
    value: any[],
    metric: PrometheusMetric
}

export interface PrometheusMetric {
    job_id: string,
    task_name: string
}