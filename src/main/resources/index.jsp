<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <link rel="icon" type="image/png" href="<%=request.getContextPath()%>/public/img/logo.png"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/public/login-style.css"/>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/public/css/bootstrap.min.css"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Downloader</title>
    <script type="text/javascript" src="<%=request.getContextPath()%>/public/jquery-3.0.0.min.js"
            charset="utf-8"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/public/bootstrap.min.js"
            charset="utf-8"></script>
    <script type="text/javascript" charset="utf-8">

        function onTestClick() {
            var info = $('#test-label')[0];
            info.style.display = 'none';
            $.ajax({
                type: 'GET',
                url: 'service/download/test',
                success: function (json) {
                    info.innerText = json.test + ' ' + json.port;
                    info.style.display = 'block';
                }
            });
        }

        function onDownloadClick() {
            var info = $('#test-label')[0];
            var progress = $('#progress-label')[0];
            progress.style.display = 'block';
            info.style.display = 'none';
            $.ajax({
                type: 'POST',
                url: 'service/download',
                data: new FormData($('#downloadForm')[0]),
                processData: false,
                contentType: false,
                success: function () {
                    info.innerText = 'SUCCESS';
                    progress.style.display = 'none';
                    info.style.display = 'block';
                }
            });
        }

        function addressChange(newValue) {
            $('#urlType')[0].value = newValue;
            $('button#adValue')[0].innerHTML = newValue + ' <span class="caret">';
        }

    </script>
</head>
<body>
<div class="overlay">
    <table class="page_center">
        <tbody>
        <tr>
            <td width="33%">&nbsp;</td>
            <td width="33%">
                <div>
                    <h1 class="text-center">Image downloader</h1>
                    <form method="post" class="form-horizontal" id="downloadForm">
                        <div class="form-group">
                            <label for="saveDir" class="col-sm-2 control-label">Directory</label>
                            <div class="col-sm-10">
                                <div class="input-group-height input-group">
                                    <input name="saveDir" type="text" class="form-control" id="saveDir"
                                           placeholder="Save directory">
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="inputAddress" class="col-sm-2 control-label">Address</label>
                            <div class="col-sm-10">
                                <div class="input-group">
                                    <div class="input-group-btn">
                                        <button type="button" class="btn btn-default dropdown-toggle" id="adValue"
                                                data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                                            JOY<span class="caret"></span></button>
                                        <ul class="dropdown-menu">
                                            <li><a onclick="addressChange('JOY')">JOY</a></li>
                                            <li role="separator" class="divider"></li>
                                            <li><a onclick="addressChange('SAN')">SAN</a></li>
                                            <li role="separator" class="divider"></li>
                                            <li><a onclick="addressChange('CUS')">CUS</a></li>
                                        </ul>
                                    </div>
                                    <input type="text" class="form-control" aria-label="..." id="inputAddress"
                                           placeholder="Postfix" name="inputAddress">
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="inputStartPage" class="col-sm-2 control-label">Start</label>
                            <div class="col-sm-10">
                                <div class="input-group-height input-group">
                                    <input name="startPage" type="text" class="form-control" id="inputStartPage"
                                           placeholder="Start page">
                                </div>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="endPage" class="col-sm-2 control-label">End</label>
                            <div class="col-sm-10">
                                <div class="input-group-height input-group">
                                    <input name="endPage" type="text" class="form-control" id="endPage"
                                           placeholder="End page">
                                </div>
                            </div>
                        </div>
                        <input name="urlType" type="hidden" class="form-control" id="urlType" value="JOY">
                    </form>
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="button" class="btn btn-primary" onclick="onDownloadClick()">Download</button>
                        <button type="reset" class="btn btn-warning" onclick="onTestClick()">Test</button>
                    </div>
                    <div class="row">
                        <div class="col-sm-offset-2 col-sm-10">
                            <div id="test-label" class="label label-info" style="display: none">
                            </div>
                            &nbsp;
                            <div id="progress-label" class="progress" style="display: none">
                                <div class="progress-bar progress-bar-striped active" role="progressbar"
                                     aria-valuenow="100"
                                     aria-valuemin="0" aria-valuemax="100" style="width: 100%">
                                    <span class="sr-only">Process</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </td>
            <td width="33%">&nbsp;</td>
        </tr>
        </tbody>
    </table>
</div>
</body>
</html>