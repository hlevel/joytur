layui.use('form', function(){
    var $ = layui.jquery;
    var form = layui.form;
    form.verify({
        // 验证金额
        amount: function (val, item) {
            var reg = new RegExp('^[0-9]+(.[0-9]{0,2})?$');
            var message = $(item).attr('error-msg');
            if (!reg.test(val)) {
                if (message == undefined || message == '') {
                    return '请输入正确金额';
                }
                return message;
            }
        },
        // 验证字符串
        string: function (val, item) {
            var message = $(item).attr('error-msg');
            if (val.length < item.min || val.length > item.max) {
                if (message == undefined || message == '') {
                    return '请输入' + item.min + '-' + item.max + '长度的字符串';
                }
                return message;
            }
        }
    });
});