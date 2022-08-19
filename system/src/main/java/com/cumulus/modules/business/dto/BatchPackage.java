package com.cumulus.modules.business.dto;

import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * 删除参数包装类
 *
 * @author zhangxq
 */
@Getter
@Setter
public class BatchPackage {

    /**
     * 文件名
     */
    private String name;

    /**
     * id列表
     */
    private Set<Long> ids;

    /**
     * 是否删除全部
     */
    private boolean all;

    public static class BatchPackageInt {

        /**
         * 文件名
         */
        private String name;

        /**
         * id列表
         */
        private Set<Integer> ids;

        /**
         * 是否删除全部
         */
        private boolean all;

        public Set<Integer> getIds() {
            return ids;
        }

        public void setIds(Set<Integer> ids) {
            this.ids = ids;
        }

        public boolean isAll() {
            return all;
        }

        public void setAll(boolean all) {
            this.all = all;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class BatchPackageString {

        /**
         * 文件名
         */
        private String name;

        /**
         * id列表
         */
        private Set<String> ids;

        /**
         * 是否删除全部
         */
        private boolean all;

        public Set<String> getIds() {
            return ids;
        }

        public void setIds(Set<String> ids) {
            this.ids = ids;
        }

        public boolean isAll() {
            return all;
        }

        public void setAll(boolean all) {
            this.all = all;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
