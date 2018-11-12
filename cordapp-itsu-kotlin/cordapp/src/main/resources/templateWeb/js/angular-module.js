"use strict";

const app = angular.module('demoAppModule', ['ui.bootstrap']);

// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('DemoAppController', function($http, $location, $uibModal) {
    const demoApp = this;

    // We identify the node.
    const apiBaseURL = "/api/template/";
    let peers = [];

    demoApp.openCreateProjectModal = () => {
        const modalInstance = $uibModal.open({
            templateUrl: 'demoAppModal-create-project.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });
        modalInstance.result.then(() => {}, () => {});
    };

    demoApp.openCloseProjectModal = () => {
            const modalInstance = $uibModal.open({
                templateUrl: 'demoAppModal-close-project.html',
                controller: 'ModalInstanceCtrl-close',
                controllerAs: 'modalInstance',
                resolve: {
                    demoApp: () => demoApp,
                    apiBaseURL: () => apiBaseURL,
                    peers: () => peers
                }
            });
            modalInstance.result.then(() => {}, () => {});
        };

    demoApp.openCreateAgreementModal = () => {
                const modalInstance = $uibModal.open({
                    templateUrl: 'demoAppModal-create-agreement.html',
                    controller: 'ModalInstanceCtrl-create-agreement',
                    controllerAs: 'modalInstance',
                    resolve: {
                        demoApp: () => demoApp,
                        apiBaseURL: () => apiBaseURL,
                        peers: () => peers
                    }
                });
                modalInstance.result.then(() => {}, () => {});
            };


    demoApp.getProjects = () => $http.get(apiBaseURL + "getProjects")
        .then((response) => demoApp.projects = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    demoApp.getAgreements = () => $http.get(apiBaseURL + "getSecurityAgreements")
        .then((response) => demoApp.agreements = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    demoApp.getProjects();
    demoApp.getAgreements();
});

app.controller('ModalInstanceCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    modalInstance.create = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            const createProjectEndpoint = `${apiBaseURL}CreateProject?ProjectName=${modalInstance.form.value1}&ProjectValue=${modalInstance.form.value2}&EstimatedProjectCost=${modalInstance.form.value3}&SecurityTrustee=${modalInstance.form.value4}&Bank=${modalInstance.form.value5}&Offtaker=${modalInstance.form.value6}&SPV=${modalInstance.form.value7}`;

            $http.put(createProjectEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getProjects();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        modalInstanceTwo.result.then(() => {}, () => {});
    };

    modalInstance.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return false;
    }
});

app.controller('ModalInstanceCtrl-create-agreement', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    modalInstance.create = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();
            const createAgreementEndpoint = `${apiBaseURL}CreateSecurityAgreement?ProjectName=${modalInstance.form.projectnameagreement}`;

            // Create PO and handle success / fail responses.
            $http.put(createAgreementEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getAgreements();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    modalInstance.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return false;
    }
});

app.controller('ModalInstanceCtrl-close', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    modalInstance.create = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            const closeProjectEndpoint = `${apiBaseURL}CloseProjectAdvanced?ProjectName=${modalInstance.form.projectnameclose}&ProjectCostTillDate=${modalInstance.form.projectcosttilldate}&ProjectCashFlow=${modalInstance.form.projectcashflow}`;
            $http.put(closeProjectEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getProjects();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getProjects();
                }
            );
        }
    };

    modalInstance.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return false;
    }
});

// Controller for success/fail modal dialogue.
app.controller('messageCtrl', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    modalInstanceTwo.message = message.data;
});