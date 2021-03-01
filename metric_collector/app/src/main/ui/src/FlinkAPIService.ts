import axios from 'axios';
import {Job} from "./models/FlinkApiResponse/Job";

export function uploadJar(jar: File, ip: string): Promise<any> {
    return jar.arrayBuffer().then(buffer => {
        const blob = new Blob([buffer], { type: jar.type });
        let fd = new FormData();
        fd.append('jarfile', blob, jar.name);
        return axios.post(ip + ':30881/jars/upload',
            fd
            , {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            }
        )
    })
}

export function getJob(jobId: string = '', ip: string = ''): Promise<any> {
    return axios.get(ip + ':30881/jobs/' + jobId);
}