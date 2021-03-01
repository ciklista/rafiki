export interface Result {
    taskName: string,
    operatorParallelism: number,
    maxThroughputArray: number[],
    avgMaxThroughput: number,
    highestMaxThroughput: number,
    operatorPosition: number
}