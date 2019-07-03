package cn.joytur.common.mvc.dto;

/**
 * 返回上传的bean
 */
public class UploadBean<T> {

	int errno;
    T data;

    public int getErrno() {
        return errno;
    }

    public void setErrno(int errno) {
        this.errno = errno;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
