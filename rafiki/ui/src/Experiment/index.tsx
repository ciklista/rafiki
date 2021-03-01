import React, {useEffect, useRef, useState} from 'react';
import { useLocation, useHistory} from 'react-router-dom';
import JobGraph from '../Experiment-Graph';
import './Experiment.css';
import {
    Dialog,
    DialogContent,
    DialogContentText,
    Table, TableBody,
    TableCell,
    TableContainer,
    TableHead,
    TableRow,
    TextField
} from "@material-ui/core";
import {getJob} from "../FlinkAPIService";
import {Job, JobVertice} from "../models/FlinkApiResponse/Job";
import Experiment from "../models/Experiment";
import {getOperatorRecordsIn, getOperatorsRecordsOut} from "../PrometheusService";
import {Result} from "../models/Result";
import {getExperimentResult} from "../MetricCollectorService";
import {PrometheusData, PrometheusResult} from "../models/Prometheus";
import { Bar } from 'react-chartjs-2';

export default function Server(props: any) {
    const state: any  = useLocation<any>();
    const experiment: Experiment = state.state;
    const history = useHistory();

    const [experimentResult, setExperimentResult] = useState<Result[]>([]);
    const [tableData, setTableData] = useState<any[]>([]);
    const [chartData, setChartData] = useState<any>({})

    const [operatorNames, setOperatorNames] = useState<string[]>([]);
    const [operatorThroughPut, setOperatorThroughPut] = useState<number[]>([]);
    const [operatorParallelism, setOperatorParallelism] = useState<number[]>([]);

    if (!experiment) history.push('/');
    const [jobId, setJobId] = useState<string>();
    const [job, setJob] = useState<Job>();
    const [showJobInput, setShowJobInput] = useState<boolean>(true);
    const initialElements: any = [];
    const [elements, setElements] = useState(initialElements);
    const configInput = useRef(null);
    const [showIdError, setIdError] = useState<boolean>(false);

    let lastHoveredTableRow: number;
    let lastHoveredTableCol: number;

    useEffect(() => {
        setElements(generateGraphElements());
    }, [operatorThroughPut, operatorParallelism, operatorNames]);

    useEffect(() => {
        setTableData(createTableData());
    }, [experimentResult])

    useEffect(() => {
        if (jobId) {
            const intervall = setInterval(() => {
                let temp_throughput: number[] = [];
                getOperatorsRecordsOut(experiment.ip_adress, jobId).then((r) => {
                    const data: PrometheusData = r.data.data;
                    temp_throughput = operatorNames.map((name: string) => {
                        const operator: PrometheusResult | undefined = data.result.find((result: PrometheusResult) => {
                           return result.metric.task_name === name || result.metric.task_name === name.replace(' ', '_')
                        });
                        return operator?.value[1];
                    });
                    const lastOperatorName = operatorNames[operatorNames.length - 1]
                    getOperatorRecordsIn(experiment.ip_adress, jobId, lastOperatorName).then(r => {
                        const data: PrometheusData = r.data.data;
                        const lastOperatorsValue = data.result.find((result: PrometheusResult) => {
                            return result.metric.task_name === lastOperatorName || result.metric.task_name === lastOperatorName.replace(' ', '_')
                        });
                        temp_throughput[temp_throughput.length - 1] = lastOperatorsValue?.value[1];
                        setOperatorThroughPut(temp_throughput);
                    });
                });
            }, 1000)
            return () => clearInterval(intervall);
        }
    },[jobId, experiment.ip_adress, operatorNames]);

    function submitJobId(e: any) {
        e.preventDefault();
        getJob(jobId, experiment.ip_adress)
            .then(r => {
                const r_job: Job = r.data;
                setJob(r_job);
                setShowJobInput(false);
                setOperatorNames(getOperatorNames(r_job));
                getExperimentResults();
                setOperatorParallelism(getOperatorParallelism(r_job));
            })
            .catch(() => {
                setIdError(true);
            });
    }

    function setNewParallelism(e: any) {
        e.preventDefault();
        if (!configInput || !configInput.current) return;
        // @ts-ignore
        const config = configInput.current.value;
        let r_operatorParallelism = config.split(',');
        if (r_operatorParallelism.length !== getOperatorParallelism(job).length) return;
        setOperatorParallelism(r_operatorParallelism.map((conf: any) => parseInt(conf)));
    }

    function createTableData() {
        const maxParallelism = getMaxParallelism();
        return operatorNames.map((name: string) => {
            return [...Array(maxParallelism).keys()].map((parallelism: number) => {
                const throughput = experimentResult.find((result: Result) => {
                    return result.taskName === name && result.operatorParallelism === parallelism +1;
                });
                if (!throughput) {
                    return '-';
                }
                return `${throughput.highestMaxThroughput}${throughput.backpressureConditionHolds? '' : '*'}`;
            });
        });
    }

    function getMaxParallelism(): number {
        const resultParallelisms = experimentResult.map((result: Result) => {
            return result.operatorParallelism;
        });
        return Math.max(...resultParallelisms);
    }

    function generateGraphElements(): any {
        return operatorParallelism.flatMap((operator: number, index: number, array) =>
            [...Array(operator).keys()].flatMap((suboperator:number) => {
                let nodesAndEdges: any[] = [];
                if (index > 0) {
                    nodesAndEdges.push(...[...Array(array[index-1]).keys()].map((prevOperator: number) => {
                        return {
                            id: 'e' + index + suboperator.toString() + (index-1).toString() + prevOperator.toString(),
                            type: 'solid',
                            target: operatorNames[index] + suboperator.toString(),
                            source: operatorNames[index - 1] + prevOperator.toString()
                        }
                    }));
                }
                const throughput = operatorThroughPut[index];
                const capacity = getCapacityPercentage(operatorNames[index], operatorParallelism[index], throughput);
                let backgroundColor;
                if (capacity < 80 && capacity > 50) {
                    backgroundColor = 'rgba(245, 158, 11, 1)'
                } else if (capacity > 80) {
                    backgroundColor = 'rgba(239, 68, 68, 1)'
                } else {
                    backgroundColor = 'rgba(52, 211, 153, 1)'
                }
                return [...nodesAndEdges, {
                    position: { x: 0, y: 0 },
                    id: operatorNames[index] + suboperator.toString(),
                    data: {
                        label: `${operatorNames[index]}-${suboperator.toString()}
                         ${throughput ? 'Throughput: ' + Math.round(throughput) : ''}
                         ${capacity > 0 ? 'Capacity: ' + Math.round(capacity) + '%' : ''}
                         `
                    },
                    style: {
                        backgroundColor
                    }
                }];
            })
        );
    }

    function getOperatorNames(job: Job): string[] {
        return job.vertices.map((vertice: JobVertice) => {
            return vertice.name
        });
    }

    function getOperatorParallelism(job: Job | undefined) {
        if (!job) return [];
        return job.vertices.map((vertice: JobVertice) => {
            return vertice.parallelism
        });
    }

    function getExperimentResults() {
        getExperimentResult(experiment.jar_id).then((res: any) => {
            setExperimentResult(res.data);
        });
    }

    function getCapacityPercentage(operatorName: string, operatorParallelism: number, currentThroughput: number): number {
        const operatorResult: Result | undefined = experimentResult.find((result: Result) => {
            return result.taskName === operatorName && operatorParallelism === result.operatorParallelism
        });
        if (operatorResult) {
            return currentThroughput / operatorResult.highestMaxThroughput * 100
        } else {
            return 0;
        }
    }

    function generateChartData(row: number, col: number) {
        const operatorName = operatorNames[row];
        const operatorResult: Result | undefined = experimentResult.find((result: Result) => {
            return result.taskName === operatorName && result.operatorParallelism === col+1;
        });
        const labels = operatorResult?.maxThroughputArray.map((throughput: number, index: number) => {
            return `Experiment ${index+1}`;
        });
        const data = {
            labels: labels,
            datasets: [{
                label: `Operator ${operatorName} at parallelism ${col+1}`,
                data: operatorResult?.maxThroughputArray
            }]
        };
        setChartData(data);
    }

    function onTableRowHover(row: number, col: number) {
        if (lastHoveredTableRow === row && lastHoveredTableCol === col) return;
        generateChartData(row, col);
    }

    return (
        <div className="flex-1">
            <Dialog open={showJobInput}>
                <DialogContent>
                    <DialogContentText>
                        Job Id for the Planned JarId {experiment.jar_id}:
                    </DialogContentText>
                    <form className="flex flex-row items-end justify-between" onSubmit={submitJobId}>
                        <TextField
                            autoFocus
                            margin="dense"
                            id="jobId"
                            label="Experiment Id"
                            required
                            fullWidth
                            type="text"
                            onChange={(e) => setJobId(() => e.target.value)}
                        />
                        <input className="py-1 px-3 ml-1 rounded bg-blue-300" type="submit" value="Set" />
                    </form>
                    {showIdError &&
                        <div className="text-red-600">This Job ID doesn't exist</div>
                    }
                </DialogContent>
            </Dialog>
            {!showJobInput && <div>
                <div className="flex flex-row justify-between">
                    <div className="m-2 text-2xl">{experiment?.name}</div>
                    <form className="mr-0 ml-auto w-max bg-gray-100 rounded shadow-md p-2 mt-1" onSubmit={setNewParallelism}>
                        <input type="text" className="bg-gray-200 shadow-inner z-0 p-1" ref={configInput} placeholder="New parallelism config csv" required minLength={1}/>
                        <input onClick={setNewParallelism} className="p-1 bg-blue-300 rounded ml-1 w-8" value="Set" type="submit"/>
                    </form>
                </div>
                <JobGraph initialElements={elements} test={"Test"} />
            </div>}
            {tableData[0] && !showJobInput &&
                <div>
                    <TableContainer>
                        <Table>
                            <TableHead>
                                <TableRow>
                                    <TableCell>Throughput per Parallelism</TableCell>
                                    {tableData[0].map((col: any, index: number) =>
                                        <TableCell>
                                            {index+1}
                                        </TableCell>
                                    )}
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {tableData.map((row: any[], index: number) =>
                                    <TableRow key={index}>
                                        <TableCell>
                                            {operatorNames[index]}
                                        </TableCell>
                                        {row.map((maxThroughput: number, col: number) =>
                                            <TableCell onMouseOver={() => onTableRowHover(index, col)}>
                                                {maxThroughput}
                                            </TableCell>
                                        )}
                                    </TableRow>
                                )}
                            </TableBody>
                        </Table>
                    </TableContainer>
                    <p className="text-gray-600 m-2">
                        * denotes that the operator with that parallelism never experienced backpressure so we can only
                        provide the highest throughput we measured.
                    </p>
                    <div className="h-80 w-full">
                        <Bar
                            width={10}
                            height={5}
                            options={{maintainAspectRatio: false}}
                            data={chartData}
                        />
                    </div>
                </div>
            }
        </div>
    )
}