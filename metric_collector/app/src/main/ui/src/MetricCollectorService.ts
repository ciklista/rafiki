import axios from 'axios';
const BASEPATH = 'http://localhost:8080/metric-collector/api/v1/'

export function startExperiment(jarID: string, max: number = 0, operators: string = '', clusterAddress: string = ''): Promise<any> {
    let operatorNames = operators.split(',');
    return axios.post(BASEPATH + `experiment?jarId=${jarID}&maxParallelism=${max}&clusterAddress=${clusterAddress}`,
            operatorNames,
        );
}

export function getExperimentResult(jarID: string): Promise<any> {
    return axios.get(BASEPATH + `result?jarId=${jarID}`);
}