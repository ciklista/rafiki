import axios from 'axios';

export function startExperiment(jarID: string, max: number = 0, operators: string = ''): Promise<any> {
    let operatorNames = operators.split(',');
    return axios.post(`http://localhost:8080/metric-collector/api/v1/experiment?jarid=${jarID}&max=${max}`,
            operatorNames,
        );
}