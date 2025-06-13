package io.halkyon.platform.operator.model;

public class Helm {
    private Chart chart;
    private Release release;
    private String values;

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public Release getRelease() {
        return release;
    }

    public void setRelease(Release release) {
        this.release = release;
    }

    public String getValues() {
        return values;
    }

    public void setValues(String values) {
        this.values = values;
    }

    public static class Chart {
        /**
         * The name of the chart to be installed. The name corresponds to one of the entries of the chart repository index.yaml file
         * This name is also used as the repository name and if not specified as release name
         *
         * helm repo add <chart.name> or <release.repoName> <chart.repoUrl>
         * helm install <chart.name> or <release.name> <chart.name>/<chart.name>
         */
        private String name;

        /**
         * The Url of the index.yaml file of the chart(s) from where helm can get the chart information
         */
        private String repoUrl;

        /**
         * The name of the chart's repository. If not defined, the chart name will be used
         */
        private String repoName;

        /**
         * The version of the chart
         */
        private String version;

        public String getRepoName() {
            return repoName;
        }

        public void setRepoName(String repoName) {
            this.repoName = repoName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRepoUrl() {
            return repoUrl;
        }

        public void setRepoUrl(String repoUrl) {
            this.repoUrl = repoUrl;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }

    public class Release {
        /**
         * The name of the release to be installed. If not defined, then the chart name will be used
         */
        private String name;

        /**
         * The version of the release to be installed which corresponds to the Helm AppVersion
         */
        private String version;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
