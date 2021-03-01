export interface Job {
    name: string,
    jid: string,
    state: JobState,
    vertices: JobVertice[]
}

export enum JobState {
    CANCELED= 'CANCELED',
    FAILED = 'FAILED',
    RUNNING = 'RUNNING'
}

export interface JobVertice {
    id: string,
    name: string,
    parallelism: number
}